package com.kpstv.xclipper.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.*
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.os.Build
import android.os.PowerManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.EditText
import androidx.lifecycle.MutableLiveData
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.kpstv.hvlog.HVLog
import com.kpstv.xclipper.App
import com.kpstv.xclipper.App.ACTION_INSERT_TEXT
import com.kpstv.xclipper.App.ACTION_NODE_INFO
import com.kpstv.xclipper.App.ACTION_VIEW_CLOSE
import com.kpstv.xclipper.App.EXTRA_NODE_CURSOR
import com.kpstv.xclipper.App.EXTRA_NODE_TEXT
import com.kpstv.xclipper.App.EXTRA_SERVICE_TEXT
import com.kpstv.xclipper.App.showSuggestion
import com.kpstv.xclipper.data.provider.ClipboardProvider
import com.kpstv.xclipper.extensions.logger
import com.kpstv.xclipper.extensions.utils.FirebaseUtils
import com.kpstv.xclipper.extensions.utils.KeyboardUtils.Companion.getKeyboardHeight
import com.kpstv.xclipper.extensions.utils.Utils.Companion.isSystemOverlayEnabled
import com.kpstv.xclipper.service.helper.ClipboardDetection
import com.kpstv.xclipper.service.helper.LanguageDetector
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty
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
        var currentPackage: CharSequence? = null
    }

    private val keyboardHeight: MutableLiveData<Int> = MutableLiveData()

    private fun postKeyboardValue(value: Int) {
        // TODO: Try not showing keyboard in XClipper app.
        if (keyboardHeight.value != value) keyboardHeight.postValue(value)
    }

    private lateinit var powerManager: PowerManager

    private var nodeInfo: AccessibilityNodeInfo? = null

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
        currentPackage = event?.packageName

       // logger(TAG, "$event")
        //  logger(TAG, "SourceText: ${event?.source}; Text is null: ${event?.text.isNullOrEmpty()}; $event")
        //   logger(TAG, "Actions: ${ClipboardDetection.ignoreSourceActions(event?.source?.actionList)}, List: ${event?.source?.actionList}")
        if (event?.eventType != null)
            clipboardDetector.addEvent(event.eventType)

        postKeyboardValue(getKeyboardHeight(applicationContext))

        event?.source?.apply {
            if (className == EditText::class.java.name) {
                nodeInfo = this
//                logger("ClipboardAccessibilityService", "Does this work")
                if (textSelectionStart == textSelectionEnd) {
                    val isHintShowing = if (Build.VERSION.SDK_INT >= 26) isShowingHintText else text.toString().length > textSelectionEnd
                    logger("BubbleService", "Text: $text, Cursor: $textSelectionEnd, isHint: $isHintShowing, contentDesc: $contentDescription")

                    sendDataToBubbleService(text.toString(), isHintShowing, textSelectionEnd)
                }
            }
        }

        if (powerManager.isInteractive) {
            updateScreenInteraction(true)
        } else
            updateScreenInteraction(false)

        if (event?.packageName != packageName)
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
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        HVLog.d()

        logger(TAG, "Service Connected")
        val info = AccessibilityServiceInfo()

        info.apply {
            eventTypes =
                AccessibilityEvent.TYPE_VIEW_CLICKED or AccessibilityEvent.TYPE_VIEW_FOCUSED or AccessibilityEvent.TYPE_VIEW_LONG_CLICKED or AccessibilityEvent.TYPE_VIEW_SELECTED or AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED or AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED or AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED or AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            notificationTimeout = 120
        }

        serviceInfo = info

        firebaseUtils.observeDatabaseChangeEvents()
        clipboardProvider.observeClipboardChange()

        keyboardHeight.observeForever { value ->
            logger(TAG, "Value: $value")

            /** A safe check to make sure we should check permission if we
             *  are using service related to it. */
            if (isSystemOverlayEnabled(applicationContext) && showSuggestion) {
                if (value > 100)
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

        LocalBroadcastManager.getInstance(applicationContext)
            .registerReceiver(object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    if (intent?.hasExtra(EXTRA_SERVICE_TEXT) == true) {
                        HVLog.d("Received ${::EXTRA_SERVICE_TEXT.name}")

                        val pasteData = intent.getStringExtra(EXTRA_SERVICE_TEXT)

                        if (!(nodeInfo != null && nodeInfo?.packageName != currentPackage) && context != null) {
                            Toasty.info(context, "Click on text field to capture it").show()
                            return
                        }
                        with(nodeInfo!!) {
                            refresh()
                            clipboardProvider.ignoreChange {

                                /** Saving current clipboard */
                                val currentClipText = clipboardProvider.getCurrentClip().value;

                                /** Setting data to be paste */
                                clipboardProvider.setClipboard(ClipData.newPlainText("copied", pasteData))

                                /** Make an actual paste action */
                                performAction(AccessibilityNodeInfo.ACTION_PASTE)

                                /** Restore previous clipboard */
                                clipboardProvider.setClipboard(ClipData.newPlainText(null, currentClipText))

                                HVLog.d("Pasted into current clip")
                            }
                        }
                    }
                }
            }, IntentFilter(ACTION_INSERT_TEXT))
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
}