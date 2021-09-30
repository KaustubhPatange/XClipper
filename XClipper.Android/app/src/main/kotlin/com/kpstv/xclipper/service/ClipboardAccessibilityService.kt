package com.kpstv.xclipper.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.*
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.annotation.RequiresApi
import androidx.core.os.bundleOf
import androidx.lifecycle.MutableLiveData
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.kpstv.hvlog.HVLog
import com.kpstv.xclipper.App
import com.kpstv.xclipper.App.ACTION_DISABLE_SERVICE
import com.kpstv.xclipper.App.ACTION_INSERT_TEXT
import com.kpstv.xclipper.App.ACTION_NODE_INFO
import com.kpstv.xclipper.App.ACTION_VIEW_CLOSE
import com.kpstv.xclipper.App.EXTRA_NODE_CURSOR
import com.kpstv.xclipper.App.EXTRA_NODE_TEXT
import com.kpstv.xclipper.App.EXTRA_SERVICE_TEXT
import com.kpstv.xclipper.App.EXTRA_SERVICE_TEXT_LENGTH
import com.kpstv.xclipper.App.showSuggestion
import com.kpstv.xclipper.data.provider.ClipboardProvider
import com.kpstv.xclipper.extensions.Logger
import com.kpstv.xclipper.extensions.logger
import com.kpstv.xclipper.extensions.utils.FirebaseUtils
import com.kpstv.xclipper.extensions.utils.KeyboardUtils.Companion.isKeyboardVisible
import com.kpstv.xclipper.extensions.utils.Utils.Companion.isSystemOverlayEnabled
import com.kpstv.xclipper.service.helper.ClipboardDetection
import com.kpstv.xclipper.service.helper.LanguageDetector
import com.kpstv.xclipper.ui.fragments.settings.GeneralPreference
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ClipboardAccessibilityService : AccessibilityService() {

    @Inject
    lateinit var firebaseUtils: FirebaseUtils
    @Inject
    lateinit var clipboardProvider: ClipboardProvider

    private lateinit var clipboardDetector: ClipboardDetection

    /** We will save the package name to this variable from the event. */
    companion object {
        @Volatile
        var currentPackage: CharSequence? = null

        @RequiresApi(Build.VERSION_CODES.N)
        fun disableService(context: Context) {
            LocalBroadcastManager.getInstance(context).sendBroadcast(Intent(ACTION_DISABLE_SERVICE))
        }
    }

    private val keyboardVisibility: MutableLiveData<Boolean> = MutableLiveData()

    private fun postKeyboardValue(value: Boolean) {
        if (keyboardVisibility.value != value) keyboardVisibility.postValue(value)
    }

    private lateinit var powerManager: PowerManager

    private var nodeInfo: AccessibilityNodeInfo? = null
    private var editableNode: AccessibilityNodeInfo? = null

    /**
     * TODO: Remove this unused parameter
     *
     * Indicates whether a screen is active for interaction or not.
     * If value is true -> Screen On
     *
     * This was supposed to stop all connection of Firebase when the user's
     * screen go off as a performance improvement over network.
     * There is no implementation yet but I think it's of no use since the
     * library is smart to optimize the network calls.
     */
    private val screenInteraction = MutableLiveData(true)

    private var runForNextEventAlso = false
    private val TAG = javaClass.simpleName

    override fun onCreate() {
        super.onCreate()
        powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        clipboardDetector = ClipboardDetection(LanguageDetector.getCopyForLocale(applicationContext))
        HVLog.d()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        try {
            if (event?.packageName != packageName)
                currentPackage = event?.packageName

            // logger(TAG, "$event")
            //  logger(TAG, "SourceText: ${event?.source}; Text is null: ${event?.text.isNullOrEmpty()}; $event")
            //   logger(TAG, "Actions: ${ClipboardDetection.ignoreSourceActions(event?.source?.actionList)}, List: ${event?.source?.actionList}")
            if (event?.eventType != null)
                clipboardDetector.addEvent(event.eventType)

            if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED)
                postKeyboardValue(isKeyboardVisible(applicationContext))

            val source = event?.source
            if (source != null) {
                var node: AccessibilityNodeInfo = source
                if (!node.isEditable) recursivelyFindRequiredNodeForSuggestion(node)?.let { node = it }
                with(node) {
                    if (isEditable) editableNode = this
                    if (textSelectionStart == textSelectionEnd && text != null) {
                        sendDataToBubbleService(text.toString(), isHintVisible, textSelectionEnd)
                    }
                }
                nodeInfo = node
                if (editableNode?.packageName != currentPackage) editableNode = null
            }

            if (powerManager.isInteractive) {
                updateScreenInteraction(true)
            } else
                updateScreenInteraction(false)

            if (event?.eventType == AccessibilityEvent.TYPE_VIEW_CLICKED && event.packageName != packageName)
                LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(Intent(ACTION_VIEW_CLOSE))

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                && clipboardDetector.getSupportedEventTypes(event) && !isPackageBlacklisted(event?.packageName)
            ) {
                runForNextEventAlso = true
                logger(TAG, "Running for first time")
                runActivity(FLAG_ACTIVITY_NEW_TASK)
                return
            }

            if (runForNextEventAlso) {
                logger(TAG, "Running for second time")
                runForNextEventAlso = false
                runActivity(FLAG_ACTIVITY_NEW_TASK)
            }
        } catch (e: Exception) {
            Logger.w(e, "Accessibility Crash")
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        HVLog.d()

        logger(TAG, "Service Connected")
        val info = AccessibilityServiceInfo()

        info.apply {
            eventTypes =
                AccessibilityEvent.TYPE_VIEW_CLICKED or AccessibilityEvent.TYPE_VIEW_FOCUSED or AccessibilityEvent.TYPE_VIEW_LONG_CLICKED or AccessibilityEvent.TYPE_VIEW_SELECTED or AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED or AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED or AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED or AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_ALL_MASK
            notificationTimeout = 120
        }

        serviceInfo = info

        firebaseUtils.observeDatabaseChangeEvents()
        clipboardProvider.observeClipboardChange()

        keyboardVisibility.observeForever { visible ->
            logger(TAG, "Value: $visible")

            /** A safe check to make sure we should check permission if we
             *  are using service related to it. */
            if (isSystemOverlayEnabled(applicationContext) && showSuggestion) {
                if (visible)
                    try {
                        startService(Intent(applicationContext, BubbleService::class.java))
                    } catch (e: Exception) {
                        logger(TAG, "Bubble launched failed", e)
                    }
                else
                    try {
                        stopService(Intent(applicationContext, BubbleService::class.java))
                    } catch (e: Exception) {
                        logger(TAG, "Bubble launched failed", e)
                    }
            }
        }

        registerLocalBroadcast()
    }


    private val lock = Any()
    private fun runActivity(flag: Int) = synchronized(lock) {
        val intent = Intent(this, ChangeClipboardActivity::class.java)
        intent.addFlags(flag)
        startActivity(intent)
    }

    override fun onDestroy() {
        HVLog.d()

        /** Ensures that we remove database initialization observation. */
        firebaseUtils.removeDatabaseInitializationObservation()
        clipboardProvider.removeClipboardObserver()
        super.onDestroy()
    }

    override fun onInterrupt() {}

    private fun registerLocalBroadcast() {
        val actions = IntentFilter().apply {
            addAction(ACTION_INSERT_TEXT)
            addAction(ACTION_DISABLE_SERVICE)
        }
        LocalBroadcastManager.getInstance(applicationContext)
            .registerReceiver(object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    if (context == null) return
                    when (intent?.action) {
                        ACTION_INSERT_TEXT -> {
                            val editableNode = editableNode
                            val nodeInfo = nodeInfo
                            if (editableNode != null)
                                actionInsertText(editableNode, intent)
                            else if (nodeInfo != null)
                                actionInsertText(nodeInfo, intent)
                        }
                        ACTION_DISABLE_SERVICE -> @RequiresApi(Build.VERSION_CODES.N) {
                            disableSelf()
                            GeneralPreference.checkForSettings(context)
                        }
                    }
                }
            }, actions)
    }

    private fun actionInsertText(node: AccessibilityNodeInfo, intent: Intent) = with(node) {
        if (intent.hasExtra(EXTRA_SERVICE_TEXT)) {
            refresh()

            val pasteData = intent.getStringExtra(EXTRA_SERVICE_TEXT)

            if (isEditable) {
                val wordLength = intent.getIntExtra(EXTRA_SERVICE_TEXT_LENGTH, node.textSelectionEnd)
                actionPaste(pasteData, wordLength)
            } else {
                actionPaste(pasteData)
            }
        }
    }

    private fun AccessibilityNodeInfo.actionPaste(pasteData: String?, wordLength: Int = 0) {
        if (isEditable && text != null && textSelectionEnd == -1) { // empty EditText
            performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, bundleOf(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE to pasteData))
            return
        }

        clipboardProvider.ignoreChange {
            HVLog.d("Received ${::EXTRA_SERVICE_TEXT.name}, WordLength: ${wordLength}, Text: $pasteData")

            val currentClipText = clipboardProvider.getCurrentClip().value

            clipboardProvider.setClipboard(ClipData.newPlainText("copied", pasteData))

            if (isEditable && wordLength != 0 && textSelectionEnd != -1) {
                performAction(AccessibilityNodeInfo.ACTION_SET_SELECTION, Bundle().apply {
                    putInt(AccessibilityNodeInfo.ACTION_ARGUMENT_SELECTION_START_INT, textSelectionEnd - wordLength)
                    putInt(AccessibilityNodeInfo.ACTION_ARGUMENT_SELECTION_END_INT, textSelectionEnd)
                })
            }

            performAction(AccessibilityNodeInfo.ACTION_PASTE)

            clipboardProvider.setClipboard(ClipData.newPlainText(null, currentClipText))
        }
    }

    // We will find an editable node. If none of them exist it means we cannot use
    // search suggestions feature & we will directly paste the content.
    private fun recursivelyFindRequiredNodeForSuggestion(node: AccessibilityNodeInfo?) : AccessibilityNodeInfo? {
        if (node?.isEditable == true) return node
        for(i in 0 until (node?.childCount ?: 0)) {
            return recursivelyFindRequiredNodeForSuggestion(node?.getChild(i))
        }
        return null
    }

    private fun updateScreenInteraction(value: Boolean) {
        if (screenInteraction.value != value)
            screenInteraction.postValue(value)
    }

    private fun sendDataToBubbleService(text: String, isHintShowing: Boolean, cursor: Int) {
        LocalBroadcastManager.getInstance(applicationContext).apply {
            sendBroadcast(Intent(ACTION_NODE_INFO).apply {
                putExtra(EXTRA_NODE_TEXT, text)
                putExtra(EXTRA_NODE_CURSOR, cursor)
            })
        }
    }

    /**
     * Returns true if the current package name is not part of blacklist app list.
     */
    private fun isPackageBlacklisted(pkg: CharSequence?) =
        App.blackListedApps?.contains(pkg) == true


    private val AccessibilityNodeInfo.isHintVisible
        get() = if (Build.VERSION.SDK_INT >= 26) isShowingHintText else text.toString().length > textSelectionEnd
}