package com.kpstv.xclipper.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.paging.PagedList
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bsk.floatingbubblelib.FloatingBubbleConfig
import com.bsk.floatingbubblelib.FloatingBubbleService
import com.bsk.floatingbubblelib.FloatingBubbleTouch
import com.kpstv.license.Decrypt
import com.kpstv.xclipper.App.ACTION_INSERT_TEXT
import com.kpstv.xclipper.App.ACTION_VIEW_CLOSE
import com.kpstv.xclipper.App.EXTRA_SERVICE_TEXT
import com.kpstv.xclipper.R
import com.kpstv.xclipper.data.model.Clip
import com.kpstv.xclipper.data.repository.MainRepository
import kotlinx.android.synthetic.main.bubble_view.view.*
import kotlinx.android.synthetic.main.item_bubble_service.view.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance


class BubbleService : FloatingBubbleService(), KodeinAware {
    override val kodein by kodein()
    private val repository by instance<MainRepository>()
    private val TAG = javaClass.simpleName
    private lateinit var adapter: PageClipAdapter

    override fun getConfig(): FloatingBubbleConfig {

        val view = LayoutInflater.from(applicationContext).inflate(
            R.layout.bubble_view, null
        )

        /** Setting adapter and onClick to send PASTE event. */
        adapter = PageClipAdapter {
            val sendIntent = Intent(ACTION_INSERT_TEXT).apply {
                putExtra(EXTRA_SERVICE_TEXT, it)
            }
            LocalBroadcastManager.getInstance(applicationContext)
                .sendBroadcast(sendIntent)
            setState(false)
        }

        /** Pagination */
        repository.getDataSource().observeForever(pageObserver)

        view.recycler_view.layoutManager = LinearLayoutManager(this)
        view.recycler_view.adapter = adapter

        view.mainLayout.setOnClickListener {
            setState(false)
        }

        /** When a view is clicked outside the overlay this receiver should
         *  should minimize the expandable view.*/
        LocalBroadcastManager.getInstance(this).registerReceiver(
            object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    if (intent?.action == ACTION_VIEW_CLOSE)
                        setState(false)
                }
            },
            IntentFilter(ACTION_VIEW_CLOSE)
        )

        return FloatingBubbleConfig.Builder() // Set the drawable for the bubblec
            .bubbleIcon(ContextCompat.getDrawable(applicationContext, R.drawable.bubble_icon))
            .expandableView(view)
            .build()
    }


    private val pageObserver =
        Observer<PagedList<Clip>?> { adapter.submitList(it) }

    override fun onGetIntent(intent: Intent): Boolean {
        return true
    }

    override fun onDestroy() {
        repository.getDataSource().removeObserver(pageObserver)
        super.onDestroy()
    }

    class PageClipAdapter(
        val onClick: (String) -> Unit
    ) : PagedListAdapter<Clip, PageClipAdapter.PageClipHolder>(DiffUtils) {

        companion object {
            private val DiffUtils = object : DiffUtil.ItemCallback<Clip>() {
                override fun areItemsTheSame(oldItem: Clip, newItem: Clip) =
                    oldItem.id == newItem.id


                override fun areContentsTheSame(oldItem: Clip, newItem: Clip) =
                    oldItem == newItem

            }
        }

        class PageClipHolder(view: View) : RecyclerView.ViewHolder(view)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            PageClipHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.item_bubble_service, parent, false
                )
            )

        override fun onBindViewHolder(holder: PageClipHolder, position: Int) {
            val clip = getItem(position)
            with(holder.itemView) {
                ibc_textView.text = clip?.data?.Decrypt()
                ibc_textView.setOnClickListener {
                    onClick.invoke(clip?.data?.Decrypt()!!)
                }
            }
        }
    }

}