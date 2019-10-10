package com.obsez.android.lib.filechooser.demo.about

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.obsez.android.lib.filechooser.ChooserDialog
import com.obsez.android.lib.filechooser.demo.R
import com.obsez.android.lib.filechooser.demo.demo.Demo
import com.obsez.android.lib.filechooser.internals.UiUtil
import kotlinx.android.synthetic.main.activity_about.*
import kotlinx.android.synthetic.main.content_about.*
import kotlinx.android.synthetic.main.li_about_item.view.*
import java.util.ArrayList

// import kotlinx.android.synthetic.main.activity_about.*
// import kotlinx.android.synthetic.main.content_about.*
// import kotlinx.android.synthetic.main.li_about_item.view.*

class AboutActivity : AppCompatActivity() {
    /*
    var toolbar : Toolbar? = null
    var fab : FloatingActionButton? = null
    */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        /*
        toolbar = findViewById(R.id.toolbar)
        fab = findViewById(R.id.fab)
        */
        setSupportActionBar(toolbar)
        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    
        // TODO add general, readme, license tabs...
        setupUi()
    }
    
    private fun setupUi() {
        setupRecyclerView(recyclerView1)
    }
    
    private fun setupRecyclerView(rv: RecyclerView) {
        rv.apply {
            val linearLayoutManager = object : LinearLayoutManager(this.context) {
                override fun getExtraLayoutSpace(state: RecyclerView.State): Int {
                    return UiUtil.dip2px(56)
                }
            }
            this.layoutManager = linearLayoutManager //LinearLayoutManager(cxt)
            
            this.addItemDecoration(DividerItemDecoration(this.context, DividerItemDecoration.HORIZONTAL))
            //this.addDivider(R.drawable.recycler_view_divider)
            
            this.itemAnimator = DefaultItemAnimator() //adapter.animator
            //val animation = AnimationUtils.loadLayoutAnimation(this.context, RvTool.layoutAnimationResId)
            //this.layoutAnimation = animation
            
            this.adapter = MainAdapter(this@AboutActivity, aboutItems)
        }
    }
    
    class MainAdapter(private val ctx: AppCompatActivity, items: List<Items>) : RecyclerView.Adapter<MainAdapter.ViewHolder>() {
        
        var plainItems: MutableList<Item> = mutableListOf()
        
        init {
            for (it in items) {
                if (it.items.isNotEmpty())
                    it.items[0].catalog = it.title
                plainItems.addAll(it.items)
            }
        }
    
        @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            //return ViewHolder(TextView(parent.context))
            return ViewHolder(LayoutInflater.from(ctx).inflate(R.layout.li_about_item, parent, false)) { _, holder ->
                if (holder.mValueView.tag != null && holder.mValueView.tag is String) {
                    val link: String = holder.mValueView.tag as String
                    when {
                        link.startsWith("mailto:") -> {
                            ctx.startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse(link)))
                            //                            Demo.demo2(ctx,"") { paths ->
                            //                                val builder = AlertDialog.Builder(ctx, R.style.FileChooserDialogStyle)
                            //                                builder.setTitle("${paths.size} files selected:")
                            //                                    .setAdapter(ArrayAdapter(ctx, android.R.layout.simple_expandable_list_item_1, paths), null)
                            //                                    .create()
                            //                                    .show()
                            //                            }
                        }
                        link.startsWith("tel:") -> {
                            ctx.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse(link)))
                        }
                        link.startsWith("market:") -> {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse(link))
                            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or
                                Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                                Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
                            try {
                                ctx.startActivity(intent)
                            } catch (e: ActivityNotFoundException) {
                                ctx.startActivity(Intent(Intent.ACTION_VIEW,
                                    Uri.parse("http://play.google.com/store/apps/details?id=" + ctx.getPackageName())))
                            }
                        }
                        else -> ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link)))
                    }
                }
            }
        }
        
        override fun getItemCount(): Int {
            return plainItems.size
        }
        
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val it = plainItems[position]
            holder.mTitleView.text = it.title
            holder.mSubTitleView.text = it.subTitle
            holder.mValueView.text = it.value
            
            if (it.subTitle.isBlank()) {
                holder.mSubTitleView.visibility = View.GONE
            } else {
                holder.mSubTitleView.visibility = View.VISIBLE
            }
            if (!it.catalog.isNullOrBlank()) {
                holder.mCatalogView.text = it.catalog
                holder.mCatalogView.visibility = View.VISIBLE
            } else {
                holder.mCatalogView.visibility = View.GONE
            }
    
            //holder.mValueView.isClickable = !it.valueLink.isBlank()
            holder.mValueView.tag = it.valueLink
            //holder.mIconView.text = it.title
        }

        class ViewHolder(view: View, clicking: ((v: View, holder: ViewHolder) -> Unit)? = null) : RecyclerView.ViewHolder(view) {
            internal var mTitleView = view.title
            internal var mSubTitleView = view.sub_title
            internal var mValueView = view.value
            internal var mCatalogView = view.catalog
            //internal var mIconView = view.icon

            init {
                // mValueView.setOnClickListener {
                //     clicking?.invoke(it, this)
                // }
                view.row?.setOnClickListener {
                    clicking?.invoke(it, this)
                }
            }
        }
    }
    
    companion object {
        val aboutItems = listOf(
            Items("Information", listOf(
                Item("Homepage", "Goto", "https://github.com/hedzr/android-file-chooser"),
                Item("Issues", "Report to us", "https://github.com/hedzr/android-file-chooser/issues/new"),
                Item("License", "Apache 2.0", "https://github.com/hedzr/android-file-chooser/blob/master/LICENSE"),
                Item("Rate me", "Like!", "market://details?id=" + "com.obsez.android.lib.filechooser")
            
            )),
            Items("Credits", listOf(
                Item("Hedzr Yeh", "Email", "mailto:hedzrz@gmail.com", "Maintainer"),
                Item("Guiorgy Potskhishvili", "Email", "mailto:guiorgy123@gmail.com", "Maintainer"),
                Item("iqbalhood", "Email", "iqbalhood@gmail.com", "Logo and banner maker"),
                Item("More Contributors", "Goto", "https://github.com/hedzr/android-file-chooser#Acknowledges", "and supporters")
            ))
        )
    }
}

class Items(var title: String, var items: List<Item>)
class Item(var title: String, var value: String, var valueLink: String = "", var subTitle: String = "", var catalog: String? = null)
