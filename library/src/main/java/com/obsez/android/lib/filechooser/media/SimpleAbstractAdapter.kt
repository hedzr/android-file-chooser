package com.obsez.android.lib.filechooser.media

import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.support.annotation.*
import android.support.v4.content.ContextCompat
import android.support.v7.util.DiffUtil
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import com.obsez.android.lib.filechooser.internals.UiUtil
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
            if (diffCallback != null && items.isNotEmpty()) {
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


class GridItemDecoration private constructor(private val mHorizonSpan: Int, private val mVerticalSpan: Int, color: Int, private val mShowLastLine: Boolean) : RecyclerView.ItemDecoration() {
    
    private val mDivider: Drawable
    
    init {
        mDivider = ColorDrawable(color)
    }
    
    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        drawHorizontal(c, parent)
        drawVertical(c, parent)
    }
    
    private fun drawHorizontal(c: Canvas, parent: RecyclerView) {
        val childCount = parent.childCount
        
        for (i in 0 until childCount) {
            val child = parent.getChildAt(i)
            // if (isLastRaw(parent, i, getSpanCount(parent), childCount) && !mShowLastLine) {
            //     continue
            // }
            
            val params = child.layoutParams as RecyclerView.LayoutParams
            val left = child.left - params.leftMargin
            val right = child.right + params.rightMargin
            val top = child.bottom + params.bottomMargin
            val bottom = top + mHorizonSpan
            
            mDivider.setBounds(left, top, right, bottom)
            mDivider.draw(c)
        }
    }
    
    private fun drawVertical(c: Canvas, parent: RecyclerView) {
        val childCount = parent.childCount
        for (i in 0 until childCount) {
            val child = parent.getChildAt(i)
            if ((parent.getChildViewHolder(child).adapterPosition + 1) % getSpanCount(parent) == 0) {
                continue
            }
            
            val params = child.layoutParams as RecyclerView.LayoutParams
            val top = child.top - params.topMargin
            val bottom = child.bottom + params.bottomMargin + mHorizonSpan
            val left = child.right + params.rightMargin
            var right = left + mVerticalSpan
            
            if (i == childCount - 1) {
                right -= mVerticalSpan
            }
            mDivider.setBounds(left, top, right, bottom)
            mDivider.draw(c)
        }
    }
    
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val spanCount = getSpanCount(parent)
        val childCount = parent.adapter!!.itemCount
        val itemPosition = (view.layoutParams as RecyclerView.LayoutParams).viewLayoutPosition
        
        if (itemPosition < 0) {
            return
        }
        
        val column = itemPosition % spanCount
        val bottom: Int
        
        val left = column * mVerticalSpan / spanCount
        val right = mVerticalSpan - (column + 1) * mVerticalSpan / spanCount
        
        bottom = if (isLastRaw(parent, itemPosition, spanCount, childCount)) {
            if (mShowLastLine) {
                mHorizonSpan
            } else {
                0
            }
        } else {
            mHorizonSpan
        }
        outRect.set(left, 0, right, bottom)
    }
    
    /**
     * the columns count
     */
    private fun getSpanCount(parent: RecyclerView): Int {
        var mSpanCount = -1
        val layoutManager = parent.layoutManager
        if (layoutManager is GridLayoutManager) {
            mSpanCount = layoutManager.spanCount
        } else if (layoutManager is StaggeredGridLayoutManager) {
            mSpanCount = layoutManager.spanCount
        }
        return mSpanCount
    }
    
    /**
     * @param parent     RecyclerView
     * @param pos        position of the current item
     * @param spanCount  item count on every rows
     * @param childCount child count
     */
    private fun isLastRaw(parent: RecyclerView, pos: Int, spanCount: Int, childCount: Int): Boolean {
        val layoutManager = parent.layoutManager
        
        if (layoutManager is GridLayoutManager) {
            return getResult(pos, spanCount, childCount)
        } else if (layoutManager is StaggeredGridLayoutManager) {
            val orientation = layoutManager.orientation
            if (orientation == StaggeredGridLayoutManager.VERTICAL) {
                // StaggeredGridLayoutManager with vertical scrolling
                return getResult(pos, spanCount, childCount)
            } else {
                // StaggeredGridLayoutManager with horizontal scrolling
                if ((pos + 1) % spanCount == 0) {
                    return true
                }
            }
        }
        return false
    }
    
    private fun getResult(pos: Int, spanCount: Int, childCount: Int): Boolean {
        val remainCount = childCount % spanCount
        
        if (remainCount == 0) {
            if (pos >= childCount - spanCount) {
                return true //don't draw the last row
            }
        } else {
            if (pos >= childCount - childCount % spanCount) {
                return true
            }
        }
        return false
    }
    
    @Suppress("JoinDeclarationAndAssignment", "MemberVisibilityCanBePrivate")
    class Builder(private val mContext: Context) {
        private val mResources: Resources
        private var mShowLastLine: Boolean
        private var mHorizonSpan: Int
        private var mVerticalSpan: Int
        private var mColor: Int
        
        init {
            mResources = mContext.resources
            mShowLastLine = false
            mHorizonSpan = 1
            mVerticalSpan = 1
            //mColor = Color.WHITE
            mColor = Color.LTGRAY
        }
        
        fun setColorResource(@ColorRes resource: Int): Builder {
            setColor(ContextCompat.getColor(mContext, resource))
            return this
        }
        
        fun setColor(@ColorInt color: Int): Builder {
            mColor = color
            return this
        }
        
        fun setVerticalSpan(@DimenRes vertical: Int): Builder {
            this.mVerticalSpan = mResources.getDimensionPixelSize(vertical)
            return this
        }
        
        fun setVerticalSpan(mVertical: Float): Builder {
            this.mVerticalSpan = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, mVertical, mResources.getDisplayMetrics()).toInt()
            return this
        }
        
        fun setHorizontalSpan(@DimenRes horizontal: Int): Builder {
            this.mHorizonSpan = mResources.getDimensionPixelSize(horizontal)
            return this
        }
        
        fun setHorizontalSpan(horizontal: Float): Builder {
            this.mHorizonSpan = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, horizontal, mResources.getDisplayMetrics()).toInt()
            return this
        }
        
        fun setShowLastLine(show: Boolean): GridItemDecoration.Builder {
            mShowLastLine = show
            return this
        }
        
        fun build(): GridItemDecoration {
            return GridItemDecoration(mHorizonSpan, mVerticalSpan, mColor, mShowLastLine)
        }
    }
}


/**
 *
 * Horz: recyclerView.addItemDecoration(new DividerItemDecoration(ListRecycle.this, DividerItemDecoration.HORIZONTAL_LIST));
 * Vert: recyclerView.addItemDecoration(new DividerItemDecoration(ListRecycle.this, DividerItemDecoration.VERTICAL_LIST));
 * Both: recyclerView.addItemDecoration(new DividerItemDecoration(ListRecycle.this, DividerItemDecoration.BOTH_SET));
 * With Color:
 *   recyclerView.addItemDecoration(new DividerItemDecoration(ListRecycle.this, DividerItemDecoration.BOTH_SET,5,Color.BLUE));
 * Uses darawable:
 *   recyclerView.addItemDecoration(new DividerItemDecoration(ListRecycle.this, DividerItemDecoration.HORIZONTAL_LIST,R.mipmap.ic_launcher));
 *
 */
class DividerItemDecoration : RecyclerView.ItemDecoration {
    private var mPaint: Paint? = null
    private var mDrawable: Drawable? = null
    private var mDividerHeight = 1
    private var mOrientation: Int = 0
    
    
    /**
     * the default divider with 1dp and gray color
     *
     * @param context     default Context
     * @param orientation direction
     */
    constructor(context: Context, orientation: Int) {
        this.setOrientation(orientation)
        val a = context.obtainStyledAttributes(ATTRS)
        mDrawable = a.getDrawable(0)
        a.recycle()
    }
    
    /**
     * @param context     default Context
     * @param orientation direction
     * @param drawableId  the divider resource
     */
    constructor(context: Context, orientation: Int, @DrawableRes drawableId: Int) {
        this.setOrientation(orientation)
        mDrawable = ContextCompat.getDrawable(context, drawableId)
        mDividerHeight = mDrawable!!.intrinsicHeight
    }
    
    /**
     * @param context       default Context
     * @param orientation   direction
     * @param dividerHeight the divider width or height
     * @param dividerColor  the color
     */
    constructor(context: Context, orientation: Int, dividerHeight: Int, @ColorInt dividerColor: Int) {
        this.setOrientation(orientation)
        mDividerHeight = UiUtil.dip2px(dividerHeight)
        mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mPaint!!.color = dividerColor
        mPaint!!.style = Paint.Style.FILL
    }
    
    /**
     * @param orientation direction
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun setOrientation(orientation: Int) {
        if (orientation < 0 || orientation > 2)
            throw IllegalArgumentException("invalid orientation")
        mOrientation = orientation
    }
    
    
    /**
     *
     * @param outRect outRect.set(0, 0, 0, 0);
     * @param view    the view
     * @param parent  the parent of view
     * @param state   the state
     */
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)
        
        val layoutParams = view.layoutParams as RecyclerView.LayoutParams
        val itemPosition = layoutParams.viewLayoutPosition
        var childCount = parent.adapter!!.itemCount
        
        when (mOrientation) {
            BOTH_SET -> {
                val spanCount = this.getSpanCount(parent)
                when {
                    isLastRow(parent, itemPosition, spanCount, childCount) ->
                        outRect.set(0, 0, mDividerHeight, 0)
                    isLastColumn(parent, itemPosition, spanCount, childCount) ->
                        outRect.set(0, 0, 0, mDividerHeight)
                    else -> outRect.set(0, 0, mDividerHeight, mDividerHeight)
                }
            }
            VERTICAL_LIST -> {
                childCount--
                outRect.set(0, 0, if (itemPosition != childCount) mDividerHeight else 0, 0)
            }
            HORIZONTAL_LIST -> {
                childCount--
                outRect.set(0, 0, 0, if (itemPosition != childCount) mDividerHeight else 0)
            }
        }
    }
    
    /**
     * @param c
     * @param parent
     * @param state
     */
    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDraw(c, parent, state)
        
        val shader: Shader? = if (mDrawable == null && mPaint != null) {
            val width = parent.measuredWidth - parent.paddingRight
            val clr = mPaint!!.color
            // RadialGradient(width.toFloat() / 2, 1f,
            //     width.toFloat() / 2,
            //     Color.argb(210, Color.red(clr), Color.green(clr), Color.blue(clr)),
            //     Color.argb(0, 0, 0, 0),
            //     Shader.TileMode.MIRROR)
            val colors = intArrayOf(
                Color.argb(210, Color.red(clr), Color.green(clr), Color.blue(clr)),
                Color.argb(140, Color.red(clr), Color.green(clr), Color.blue(clr)),
                Color.argb(70, Color.red(clr), Color.green(clr), Color.blue(clr)),
                Color.argb(0, 0, 0, 0)
            )
            val stops = floatArrayOf(0f, 0.25f, 0.6f, 1f)
            // TODO this shader have not worked yet
            LinearGradient(0f, 0f, 0f, width.toFloat(), colors, stops, Shader.TileMode.REPEAT)
        } else null
        if (shader != null && mPaint != null) mPaint!!.shader = shader
        
        when (mOrientation) {
            VERTICAL_LIST -> drawVertical(c, parent, shader)
            HORIZONTAL_LIST -> drawHorizontal(c, parent, shader)
            else -> {
                drawHorizontal(c, parent, shader)
                drawVertical(c, parent, shader)
            }
        }
    }
    
    /**
     * @param canvas
     * @param parent
     */
    private fun drawHorizontal(canvas: Canvas, parent: RecyclerView, shader: Shader?) {
        val x = parent.paddingLeft
        val width = parent.measuredWidth - parent.paddingRight
        val childSize = parent.childCount
        
        for (i in 0 until childSize) {
            val child = parent.getChildAt(i)
            val layoutParams = child.layoutParams as RecyclerView.LayoutParams
            val y = child.bottom + layoutParams.bottomMargin
            val height = y + mDividerHeight
            
            if (mDrawable != null) {
                mDrawable!!.setBounds(x, y, width, height)
                mDrawable!!.draw(canvas)
            }
            if (mPaint != null) {
                //if (shader != null) mPaint!!.shader = shader
                canvas.drawRect(x.toFloat(), y.toFloat(), width.toFloat(), height.toFloat(), mPaint!!)
            }
        }
    }
    
    /**
     * @param canvas
     * @param parent
     */
    private fun drawVertical(canvas: Canvas, parent: RecyclerView, shader: Shader?) {
        val top = parent.paddingTop
        val bottom = parent.measuredHeight - parent.paddingBottom
        val childSize = parent.childCount
        
        for (i in 0 until childSize) {
            val child = parent.getChildAt(i)
            val layoutParams = child.layoutParams as RecyclerView.LayoutParams
            val left = child.right + layoutParams.rightMargin
            val right = left + mDividerHeight
            
            if (mDrawable != null) {
                mDrawable!!.setBounds(left, top, right, bottom)
                mDrawable!!.draw(canvas)
            }
            if (mPaint != null) {
                //if (shader != null) mPaint!!.shader = shader
                canvas.drawRect(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), mPaint!!)
            }
        }
    }
    
    
    /**
     * @param parent
     * @return
     */
    private fun getSpanCount(parent: RecyclerView): Int {
        var spanCount = -1
        val layoutManager = parent.layoutManager
        if (layoutManager is GridLayoutManager) {
            spanCount = layoutManager.spanCount
        } else if (layoutManager is StaggeredGridLayoutManager) {
            spanCount = layoutManager
                .spanCount
        }
        return spanCount
    }
    
    
    private fun isLastColumn(parent: RecyclerView, pos: Int, spanCount: Int,
                             childCount: Int): Boolean {
        var childCountLocal = childCount
        val layoutManager = parent.layoutManager
        if (layoutManager is GridLayoutManager) {
            val orientation = layoutManager.orientation
            if (orientation == StaggeredGridLayoutManager.VERTICAL) {
                if ((pos + 1) % spanCount == 0)
                    return true
            } else {
                childCountLocal -= childCountLocal % spanCount
                if (pos >= childCountLocal)
                    return true
            }
        } else if (layoutManager is StaggeredGridLayoutManager) {
            val orientation = layoutManager.orientation
            if (orientation == StaggeredGridLayoutManager.VERTICAL) {
                if ((pos + 1) % spanCount == 0)
                    return true
            } else {
                childCountLocal -= childCountLocal % spanCount
                if (pos >= childCountLocal)
                    return true
            }
        }
        return false
    }
    
    private fun isLastRow(parent: RecyclerView, pos: Int, spanCount: Int,
                          childCount: Int): Boolean {
        var childCountLocal = childCount
        val orientation: Int
        val layoutManager = parent.layoutManager
        if (layoutManager is GridLayoutManager) {
            childCountLocal -= childCountLocal % spanCount
            orientation = layoutManager.orientation
            if (orientation == StaggeredGridLayoutManager.VERTICAL) {
                // 如果是最后一行，则不需要绘制底部
                childCountLocal -= childCountLocal % spanCount
                if (pos >= childCountLocal)
                    return true
            } else {
                if ((pos + 1) % spanCount == 0)
                    return true
            }
        } else if (layoutManager is StaggeredGridLayoutManager) {
            orientation = layoutManager.orientation
            if (orientation == StaggeredGridLayoutManager.VERTICAL) {
                childCountLocal -= childCountLocal % spanCount
                if (pos >= childCountLocal)
                    return true
            } else {
                if ((pos + 1) % spanCount == 0)
                    return true
            }
        }
        return false
    }
    
    companion object {
        private val ATTRS = intArrayOf(android.R.attr.listDivider)
        const val HORIZONTAL_LIST = RecyclerView.HORIZONTAL
        const val VERTICAL_LIST = RecyclerView.VERTICAL
        const val BOTH_SET = 2
    }
}

