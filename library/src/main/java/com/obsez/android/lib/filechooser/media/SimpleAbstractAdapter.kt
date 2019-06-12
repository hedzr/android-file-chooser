package com.obsez.android.lib.filechooser.media

import android.support.annotation.LayoutRes
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import com.obsez.android.lib.filechooser.tool.addRipple
import com.obsez.android.lib.filechooser.tool.makeClickable


// https://gist.github.com/arnyigor/568035c6db9bdbeaf609b68a71834349
// https://stackoverflow.com/questions/40584424/simple-android-recyclerview-example/40584425#40584425
// https://stackoverflow.com/a/53773820/6375060
/*

class BucketsAdapter(private val listener:TasksListener? = null) : SimpleAbstractAdapter<Task>() {
    override fun getLayout(): Int {
        return R.layout.task_item_layout
    }
    
    override fun getDiffCallback(): DiffCallback<Task>? {
        return object : DiffCallback<Task>() {
            override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
                return oldItem.id == newItem.id
            }
    
            override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
                return oldItem.items == newItem.items
            }
        }
    }
    
    @SuppressLint("SetTextI18n")
    override fun bindView(item: Task, viewHolder: VH) {
        viewHolder.itemView.apply {
            val position = viewHolder.adapterPosition
            val customer = item.customer
            val customerName = if (customer != null) customer.name else ""
            tvTaskCommentTitle.text = customerName + ", #" + item.id
            tvCommentContent.text = item.taskAddress
            ivCall.setOnClickListener {
                listener?.onCallClick(position, item)
            }
            setOnClickListener {
                listener?.onItemClick(position, item)
            }
        }
    }
    
    interface TasksListener : SimpleAbstractAdapter.OnViewHolderListener<Task> {
        fun onCallClick(position: Int, item: Task)
        }
    }
}

    mAdapter = BucketsAdapter(object : BucketsAdapter.TasksListener {
        override fun onCallClick(position: Int, item:Task) {
        }
        override fun onItemClick(position: Int, item:Task) {
        }
    })
    rvTasks.adapter = mAdapter
    
    mAdapter?.setFilter(object : SimpleAbstractAdapter.SimpleAdapterFilter<MoveTask> {
            override fun onFilterItem(contains: CharSequence, item:Task): Boolean {
                return contains.toString().toLowerCase().contains(item.id?.toLowerCase().toString())
            }
    })
    mAdapter?.filter("test")
 */

@Suppress("MemberVisibilityCanBePrivate", "unused")
abstract class SimpleAbstractAdapter<T>(internal var items: ArrayList<T> = arrayListOf())
    : RecyclerView.Adapter<SimpleAbstractAdapter.VH>() {
    private var listener: OnViewHolderListener<T>? = null
    private val filter = ArrayFilter()
    private val lock = Any()
    protected abstract fun getLayout(viewType: Int = 0): Int
    protected abstract fun bindView(item: T, viewHolder: VH)
    protected open fun getDiffCallback(): DiffCallback<T>? = null
    private var onFilterObjectCallback: OnFilterObjectCallback? = null
    private var constraint: CharSequence? = ""
    
    override fun onBindViewHolder(vh: VH, position: Int) {
        getItem(position)?.let { bindView(it, vh) }
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(parent, getLayout(viewType))
    }
    
    override fun getItemCount(): Int = items.size
    
    abstract class DiffCallback<T> : DiffUtil.Callback() {
        private val mOldItems = ArrayList<T>()
        private val mNewItems = ArrayList<T>()
        
        fun setItems(oldItems: List<T>, newItems: List<T>) {
            mOldItems.clear()
            mOldItems.addAll(oldItems)
            mNewItems.clear()
            mNewItems.addAll(newItems)
        }
        
        override fun getOldListSize(): Int {
            return mOldItems.size
        }
        
        override fun getNewListSize(): Int {
            return mNewItems.size
        }
        
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return areItemsTheSame(mOldItems[oldItemPosition], mNewItems[newItemPosition])
        }
        
        abstract fun areItemsTheSame(oldItem: T, newItem: T): Boolean
        
        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return areContentsTheSame(
                mOldItems[oldItemPosition],
                mNewItems[newItemPosition]
            )
        }
        
        abstract fun areContentsTheSame(oldItem: T, newItem: T): Boolean
    }
    
    
    class VH(parent: ViewGroup, @LayoutRes layout: Int) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(layout, parent, false).apply {
            // android:background="?actionBarItemBackground"
            // android:background="?android:attr/actionBarItemBackground"
            addRipple()
            makeClickable(true)
        })
    
    
    interface OnViewHolderListener<T> {
        fun onItemClick(position: Int, item: T)
    }
    
    open fun getItem(position: Int): T? {
        return items.getOrNull(position)
    }
    
    fun getItems(): ArrayList<T> {
        return items
    }
    
    fun setViewHolderListener(listener: OnViewHolderListener<T>) {
        this.listener = listener
    }
    
    open fun addAll(list: List<T>, useDiffUtils: Boolean = true) {
        if (useDiffUtils) {
            val diffCallback = getDiffCallback()
            if (diffCallback != null && !items.isEmpty()) {
                diffCallback.setItems(items, list)
                val diffResult = DiffUtil.calculateDiff(diffCallback)
                items.clear()
                items.addAll(list)
                diffResult.dispatchUpdatesTo(this)
            } else {
                items.addAll(list)
                notifyDataSetChanged()
            }
        } else {
            items.addAll(list)
        }
    }
    
    open fun add(item: T) {
        items.add(item)
        notifyDataSetChanged()
    }
    
    open fun add(position: Int, item: T) {
        try {
            items.add(position, item)
            notifyItemInserted(position)
        } catch (e: Exception) {
        }
    }
    
    open fun remove(position: Int) {
        try {
            items.removeAt(position)
            notifyItemRemoved(position)
        } catch (e: Exception) {
        }
    }
    
    open fun remove(item: T) {
        try {
            items.remove(item)
            notifyDataSetChanged()
        } catch (e: Exception) {
        }
    }
    
    open fun clear(notify: Boolean = false) {
        items.clear()
        if (notify) {
            notifyDataSetChanged()
        }
    }
    
    
    //
    
    //
    
    
    fun setFilter(filter: SimpleAdapterFilter<T>): ArrayFilter {
        return this.filter.setFilter(filter)
    }
    
    interface SimpleAdapterFilter<T> {
        fun onFilterItem(contains: CharSequence, item: T): Boolean
    }
    
    fun convertResultToString(resultValue: Any): CharSequence {
        return filter.convertResultToString(resultValue)
    }
    
    fun filter(constraint: CharSequence) {
        this.constraint = constraint
        filter.filter(constraint)
    }
    
    fun filter(constraint: CharSequence, listener: Filter.FilterListener) {
        this.constraint = constraint
        filter.filter(constraint, listener)
    }
    
    protected fun itemToString(item: T): String? {
        return item.toString()
    }
    
    fun getFilter(): Filter {
        return filter
    }
    
    interface OnFilterObjectCallback {
        fun handle(countFilterObject: Int)
    }
    
    fun setOnFilterObjectCallback(objectCallback: OnFilterObjectCallback) {
        onFilterObjectCallback = objectCallback
    }
    
    inner class ArrayFilter : Filter() {
        private var original: ArrayList<T> = arrayListOf()
        private var filter: SimpleAdapterFilter<T> = DefaultFilter()
        private var list: ArrayList<T> = arrayListOf()
        private var values: ArrayList<T> = arrayListOf()
        
        
        fun setFilter(filter: SimpleAdapterFilter<T>): ArrayFilter {
            original = items
            this.filter = filter
            return this
        }
        
        override fun performFiltering(constraint: CharSequence?): Filter.FilterResults {
            val results = Filter.FilterResults()
            if (constraint == null || constraint.isBlank()) {
                synchronized(lock) {
                    list = original
                }
                results.values = list
                results.count = list.size
            } else {
                synchronized(lock) {
                    values = original
                }
                val result = ArrayList<T>()
                for (value in values) {
                    if (!constraint.isNullOrBlank() && value != null) {
                        if (filter.onFilterItem(constraint, value)) {
                            result.add(value)
                        }
                    } else {
                        value?.let { result.add(it) }
                    }
                }
                results.values = result
                results.count = result.size
            }
            return results
        }
        
        override fun publishResults(constraint: CharSequence, results: Filter.FilterResults) {
            items = results.values as? ArrayList<T> ?: arrayListOf()
            notifyDataSetChanged()
            onFilterObjectCallback?.handle(results.count)
        }
        
    }
    
    class DefaultFilter<T> : SimpleAdapterFilter<T> {
        override fun onFilterItem(contains: CharSequence, item: T): Boolean {
            val valueText = item.toString().toLowerCase()
            if (valueText.startsWith(contains.toString())) {
                return true
            } else {
                val words = valueText.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                for (word in words) {
                    if (word.contains(contains)) {
                        return true
                    }
                }
            }
            return false
        }
    }
}
