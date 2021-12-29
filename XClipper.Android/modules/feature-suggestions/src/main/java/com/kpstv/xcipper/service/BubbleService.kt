package com.kpstv.xcipper.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.TypedValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import com.bsk.floatingbubblelib.DefaultFloatingBubbleTouchListener
import com.bsk.floatingbubblelib.FloatingBubbleConfig
import com.bsk.floatingbubblelib.FloatingBubbleService
import com.bsk.floatingbubblelib.FloatingBubbleTouchListener
import com.kpstv.xcipper.di.action.ClipboardAccessibilityServiceActions
import com.kpstv.xcipper.di.navigation.SpecialActionsLauncher
import com.kpstv.xcipper.feature_suggestions.R
import com.kpstv.xcipper.feature_suggestions.databinding.BubbleViewBinding
import com.kpstv.xcipper.ui.adapters.PageClipAdapter
import com.kpstv.xclipper.data.model.Clip
import com.kpstv.xclipper.data.provider.ClipboardProvider
import com.kpstv.xclipper.data.repository.MainRepository
import com.kpstv.xclipper.extensions.*
import com.kpstv.xclipper.extensions.utils.ToastyUtils
import com.kpstv.xclipper.ui.dialogs.FeatureDialog
import com.kpstv.xclipper.ui.helpers.AppSettings
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BubbleService : FloatingBubbleService() {

    @Inject
    lateinit var repository: MainRepository

    @Inject
    lateinit var appSettings: AppSettings

    @Inject
    lateinit var clipboardProvider: ClipboardProvider

    @Inject
    lateinit var specialActionsLauncher: SpecialActionsLauncher

    @Inject
    lateinit var clipboardServiceActions: ClipboardAccessibilityServiceActions

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
        adapter = PageClipAdapter(
            clipboardProvider = clipboardProvider,
            specialActionsLauncher = specialActionsLauncher,
            onClick = { text ->
                setState(false)
                clipboardServiceActions.sendClipboardInsertText(
                    wordLength = currentWord.length,
                    text = text
                )
            }
        )

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
        LocalBroadcastManager.getInstance(this).registerReceiver(bubbleBroadcastReceiver, filter)

        val actionBarSize = context.getRawDataAttr(android.R.attr.actionBarSize).run {
            TypedValue.complexToDimensionPixelSize(this, resources.displayMetrics)
        }
        return FloatingBubbleConfig.Builder()
            .bubbleIcon(ContextCompat.getDrawable(applicationContext, R.drawable.bubble_icon))
            .expandableView(binding.root)
            .physicsEnabled(true)
            .bubbleYOffset(actionBarSize)
            .build()
    }

    private val bubbleBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == null) return
            when (intent.action) {
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
    }

    override fun getTouchListener(): FloatingBubbleTouchListener {
        return object : DefaultFloatingBubbleTouchListener() {
            override fun onTap(expanded: Boolean) {
                if (!showSearchFeatureDialog(context, appSettings)) {
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

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return try {
            super.onStartCommand(intent, flags, startId)
        } catch (e: OutOfMemoryError) {
            ToastyUtils.showWarning(this, getString(R.string.bubble_oom_error))
            Logger.w(e, "Non-fatal: Out of memory issue in BubbleService")
            stopSelf()
            START_NOT_STICKY
        }
    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(bubbleBroadcastReceiver)
        repository.getDataSource().removeObserver(pageObserver)
        super.onDestroy()
    }

    override fun onLowMemory() {
        // We are low on memory stop allocating anything.
        stopSelf()
    }

    private fun showSearchFeatureDialog(context: Context, appSettings: AppSettings): Boolean {
        if (!appSettings.isBubbleOnBoardingDialogShown()) {
            FeatureDialog(context)
                .setResourceId(R.drawable.bubble_feature_suggestion_search)
                .setTitle(R.string.bubble_search_title)
                .setSubtitle(R.string.bubble_search_subtitle)
                .show()
            appSettings.setBubbleOnBoardingDialogShown(true)
            return true
        }
        return false
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
}