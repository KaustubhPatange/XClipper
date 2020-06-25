package com.kpstv.xclipper.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.*
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.os.Build
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Button
import android.widget.EditText
import androidx.lifecycle.MutableLiveData
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.kpstv.xclipper.App
import com.kpstv.xclipper.App.ACTION_INSERT_TEXT
import com.kpstv.xclipper.App.ACTION_VIEW_CLOSE
import com.kpstv.xclipper.App.EXTRA_SERVICE_TEXT
import com.kpstv.xclipper.App.showSuggestion
import com.kpstv.xclipper.data.provider.ClipboardProvider
import com.kpstv.xclipper.data.repository.MainRepository
import com.kpstv.xclipper.extensions.utils.FirebaseUtils
import com.kpstv.xclipper.extensions.utils.KeyboardUtils.Companion.getKeyboardHeight
import com.kpstv.xclipper.extensions.utils.Utils.Companion.isSystemOverlayEnabled
import com.kpstv.xclipper.extensions.utils.Utils.Companion.retrievePackageList
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance
import java.util.*


class ClipboardAccessibilityService : AccessibilityService(), KodeinAware {

    /** We will save the package name to this variable from the event. */
    companion object {
        var currentPackage: CharSequence? = null
    }

    private val keyboardHeight: MutableLiveData<Int> = MutableLiveData()

    private fun postKeyboardValue(value: Int) {
        if (keyboardHeight.value != value) keyboardHeight.postValue(value)
    }

    override val kodein by kodein()
    private val firebaseUtils by instance<FirebaseUtils>()
    private val clipboardProvider by instance<ClipboardProvider>()

    private var nodeInfo: AccessibilityNodeInfo? = null

    private var runForNextEventAlso = false

    private val TAG = javaClass.simpleName

    override fun onCreate() {
        super.onCreate()

        firebaseUtils.observeDatabaseChangeEvents()
    }

    /** Some hacks I figured out which would trigger copy/cut for Android 10 */
    private fun supportedEventTypes(event: AccessibilityEvent?): Boolean {
        /**
         * This first condition will allow to capture text from an text selection,
         * whether on chrome or somewhere else.
         *
         * eg: Press and hold a text > a pop comes with different options like
         * copy, paste, select all, etc.
         */
        return (event?.eventType == AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED
                && event.fromIndex == event.toIndex
                && event.currentItemIndex != -1)

                ||

                /**
                 * This second condition is a hack whenever someone clicks copy or cut context button,
                 * it detects this behaviour as copy.
                 *
                 * Disadvantages: Event TYPE_VIEW_CLICKED is fired whenever you touch on the screen,
                 * this means if there is a text which contains "copy" it's gonna consider that as a
                 * copy behaviour.
                 */
                (event?.eventType == AccessibilityEvent.TYPE_VIEW_CLICKED && event.text != null &&
                (event.contentDescription?.toString()?.toLowerCase(Locale.ROOT)?.contains("copy") == true
                        || event.text?.toString()?.toLowerCase(Locale.ROOT)?.contains("copy") == true
                        || event.contentDescription == "Cut"))
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        currentPackage = event?.packageName

        postKeyboardValue(getKeyboardHeight(applicationContext))

     //   Log.e(TAG, "Event: $event")

        event?.source?.apply {

            if (className == EditText::class.java.name) {
                nodeInfo = this
            }
        }

        if (event?.packageName != packageName)
            LocalBroadcastManager.getInstance(applicationContext)
                .sendBroadcast(Intent(ACTION_VIEW_CLOSE))

         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && supportedEventTypes(event) && !isPackageBlacklisted(
                 event?.packageName
             )
         ) {

             runForNextEventAlso = true

             Log.e(TAG, "Running for first time")

             runActivity(FLAG_ACTIVITY_NEW_TASK)

             return
         }

        if (runForNextEventAlso) {
            Log.e(TAG, "Running for second time")
            runForNextEventAlso = false
            runActivity(FLAG_ACTIVITY_NEW_TASK)
        }
    }


    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.e(TAG, "Service Connected")
        val info = AccessibilityServiceInfo()

        info.apply {
            eventTypes =
                AccessibilityEvent.TYPE_VIEW_CLICKED or AccessibilityEvent.TYPE_VIEW_FOCUSED or AccessibilityEvent.TYPE_VIEW_LONG_CLICKED or AccessibilityEvent.TYPE_VIEW_SELECTED or AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED or AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED or AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED or AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            notificationTimeout = 120
        }

        serviceInfo = info

        clipboardProvider.observeClipboardChange()

        retrievePackageList(applicationContext)

        keyboardHeight.observeForever { value ->
            Log.e(TAG, "Value: $value")

            /** A safe check to make sure we should check permission if we
             *  are using service related to it. */
            if (isSystemOverlayEnabled(applicationContext) && showSuggestion) {
                if (value > 100)
                    startService(Intent(applicationContext, BubbleService::class.java))
                else
                    stopService(Intent(applicationContext, BubbleService::class.java))
            }
        }


        LocalBroadcastManager.getInstance(applicationContext)
            .registerReceiver(object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    if (intent?.hasExtra(EXTRA_SERVICE_TEXT) == true) {
                        val pasteData = intent.getStringExtra(EXTRA_SERVICE_TEXT)

                        if (nodeInfo != null) {
                            with(nodeInfo!!) {
                                refresh()
                                clipboardProvider.ignoreChange {

                                    /** Saving current clipboard */
                                    val currentClipboard = clipboardProvider.getClipboard()

                                    /** Setting data to be paste */
                                    clipboardProvider.setClipboard(
                                        ClipData.newPlainText(
                                            "copied",
                                            pasteData
                                        )
                                    )

                                    /** Make an actual paste request */
                                    performAction(AccessibilityNodeInfo.ACTION_PASTE)

                                    /** Restore previous clipboard */
                                    clipboardProvider.setClipboard(currentClipboard)
                                }
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
        /** Ensures that we remove database initialization observation. */
        firebaseUtils.removeDatabaseInitializationObservation()
        firebaseUtils.removeDataChangeObservation()
        super.onDestroy()
    }

    override fun onInterrupt() {}

    /**
     * Returns true if the current package name is not part of blacklist app list.
     */
    private fun isPackageBlacklisted(pkg: CharSequence?) =
        App.blackListedApps?.contains(pkg) == true
}