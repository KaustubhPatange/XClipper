package com.kpstv.xclipper.service

import android.content.*
import android.util.TypedValue
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
import com.bsk.floatingbubblelib.DefaultFloatingBubbleTouchListener
import com.bsk.floatingbubblelib.FloatingBubbleConfig
import com.bsk.floatingbubblelib.FloatingBubbleService
import com.bsk.floatingbubblelib.FloatingBubbleTouchListener
import com.kpstv.xclipper.R
import com.kpstv.xclipper.data.model.Clip
import com.kpstv.xclipper.data.provider.ClipboardProvider
import com.kpstv.xclipper.data.provider.PreferenceProvider
import com.kpstv.xclipper.data.repository.MainRepository
import com.kpstv.xclipper.databinding.BubbleViewBinding
import com.kpstv.xclipper.databinding.ItemBubbleServiceBinding
import com.kpstv.xclipper.extensions.*
import com.kpstv.xclipper.extensions.utils.Utils
import com.kpstv.xclipper.extensions.utils.Utils.Companion.showSearchFeatureDialog
import com.kpstv.xclipper.ui.activities.SpecialActions
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty
import javax.inject.Inject

@AndroidEntryPoint
class BubbleService : FloatingBubbleService() {

    @Inject
    lateinit var repository: MainRepository
    @Inject
    lateinit var preferenceProvider: PreferenceProvider
    @Inject
    lateinit var clipboardProvider: ClipboardProvider

    private val TAG = javaClass.simpleName
    private lateinit var adapter: PageClipAdapter

    private var currentWord: String = ""
    private var shouldResubscribe: Boolean = false // only when currentWord changes

    private lateinit var binding: BubbleViewBinding

    companion object {
        private const val ACTION_VIEW_CLOSE = "com.kpstv.xclipper.action_view_close"

        private const val ACTION_NODE_INFO = "com.kpstv.xclipper.action_node_text"
        private const val EXTRA_NODE_CURSOR = "com.kpstv.xclipper.extra_node_cursor"
        private const val EXTRA_NODE_TEXT = "com.kpstv.xclipper.extra_node_text"
    }

    override fun getConfig(): FloatingBubbleConfig {

        binding = BubbleViewBinding.inflate(applicationContext.layoutInflater())

        /** Setting adapter and onClick to send PASTE event. */
        adapter = PageClipAdapter(clipboardProvider) { text ->
            setState(false)
            ClipboardAccessibilityService.Actions.sendClipboardInsertText(applicationContext, currentWord.length, text)
        }

        subscribeSuggestions()

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        binding.root.setOnClickListener {
            setState(false)
        }

        binding.btnClear.setOnClickListener {
            clearFilters()
            subscribeSuggestions()
        }

        /** When a view is clicked outside the overlay this receiver should
         *  should minimize the expandable view.*/
        val filter = IntentFilter().apply {
            addAction(ACTION_VIEW_CLOSE)
            addAction(ACTION_NODE_INFO)
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(
            object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    if (intent?.action == null) return
                    when(intent.action) {
                        ACTION_VIEW_CLOSE -> setState(false)
                        ACTION_NODE_INFO -> {
                            val currentText = intent.getStringExtra(EXTRA_NODE_TEXT) ?: ""
                            val currentPosition = intent.getIntExtra(EXTRA_NODE_CURSOR, -1)

                            if (currentPosition <= 0 || currentText.length < currentPosition) {
                                clearFilters()
                            } else {
                                // 6th pos
                                // this is| an example
                                val compiled = "\\s+".toRegex()
                                val upto = currentText.substring(0, currentPosition)
                                currentWord = upto.split(compiled).last()
                                binding.tvQuery.text = "Query: $currentWord"
                                binding.btnClear.show()

                                shouldResubscribe = true
                            }
                        }
                    }
                }
            },
            filter
        )

        val actionBarSize = Utils.getDataFromAttr(context, android.R.attr.actionBarSize).run {
            TypedValue.complexToDimensionPixelSize(this, resources.displayMetrics)
        }
        return FloatingBubbleConfig.Builder()
            .bubbleIcon(ContextCompat.getDrawable(applicationContext, R.drawable.bubble_icon))
            .expandableView(binding.root)
            .physicsEnabled(true)
            .bubbleYOffset(actionBarSize)
            .build()
    }

    override fun getTouchListener(): FloatingBubbleTouchListener {
        return object : DefaultFloatingBubbleTouchListener() {
            override fun onTap(expanded: Boolean) {
                if (!showSearchFeatureDialog(context, preferenceProvider)) {
                    if (expanded && shouldResubscribe) subscribeSuggestions()
                } else {
                    setState(false)
                }
            }
            override fun onRemove() {
                stopSelf()
            }
        }
    }

    private fun subscribeSuggestions() {
        repository.getDataSource(currentWord).removeObserver(pageObserver)
        repository.getDataSource(currentWord).observeForever(pageObserver)
    }

    private fun clearFilters() {
        currentWord = ""
        binding.tvQuery.text = ""
        binding.btnClear.hide()
    }

    private val pageObserver = Observer<PagedList<Clip>?> {
        adapter.submitList(it)
    }

    override fun onGetIntent(intent: Intent): Boolean {
        return true
    }

    override fun onDestroy() {
        repository.getDataSource().removeObserver(pageObserver)
        super.onDestroy()
    }

    object Actions {
        fun sendCloseState(context: Context) {
            context.broadcastManager().sendBroadcast(Intent(ACTION_VIEW_CLOSE))
        }
        fun sendNodeInfo(context: Context, nodeText: String, cursorPosition: Int) {
            context.broadcastManager().sendBroadcast(Intent(ACTION_NODE_INFO).apply {
                putExtra(EXTRA_NODE_TEXT, nodeText)
                putExtra(EXTRA_NODE_CURSOR, cursorPosition)
            })
        }
    }

    class PageClipAdapter(private val clipboardProvider: ClipboardProvider, val onClick: (String) -> Unit) :
        PagedListAdapter<Clip, PageClipAdapter.PageClipHolder>(DiffUtils.asConfig()) {

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
            PageClipHolder(ItemBubbleServiceBinding.inflate(parent.context.layoutInflater(), parent, false).root)

        override fun onBindViewHolder(holder: PageClipHolder, position: Int) {
            val clip = getItem(position)
            with(ItemBubbleServiceBinding.bind(holder.itemView)) {

                /** This will show a small line which indicates this is a pinned clip. */
                if (clip?.isPinned == true)
                    ibcPinView.show()
                else
                    ibcPinView.hide()

                if (clipboardProvider.getCurrentClip().value == clip?.data)
                    ibcTextView.setTextColor(ContextCompat.getColor(ibcTextView.context, R.color.colorSelectedClip))
                else
                    ibcTextView.setTextColor(ContextCompat.getColor(ibcTextView.context, R.color.white))

                ibcTextView.text = clip?.data
                ibcTextView.setOnClickListener {
                    onClick.invoke(clip?.data!!)
                }
                ibcTextView.setOnLongClickListener {
                    SpecialActions.launch(root.context, clip?.data!!)
                    true
                }
                btnCopy.setOnClickListener {
                    clipboardProvider.setClipboard(ClipData.newPlainText("Copied", clip?.data!!))
                    Toasty.info(root.context, root.context.getString(R.string.ctc)).show()
                }
            }
        }
    }
}