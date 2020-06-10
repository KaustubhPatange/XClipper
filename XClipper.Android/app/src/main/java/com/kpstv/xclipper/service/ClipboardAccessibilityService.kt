package com.kpstv.xclipper.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.ColorStateList
import android.graphics.PixelFormat
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.kpstv.xclipper.App
import com.kpstv.xclipper.App.ACTION_INSERT_TEXT
import com.kpstv.xclipper.App.EXTRA_SERVICE_TEXT
import com.kpstv.xclipper.R
import com.kpstv.xclipper.data.provider.ClipboardProvider
import com.kpstv.xclipper.data.repository.MainRepository
import com.kpstv.xclipper.extensions.collapse
import com.kpstv.xclipper.extensions.show
import com.kpstv.xclipper.extensions.utils.FirebaseUtils
import com.kpstv.xclipper.extensions.utils.KeyboardUtils.Companion.getKeyboardHeight
import com.kpstv.xclipper.extensions.utils.Utils.Companion.retrievePackageList
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance


class ClipboardAccessibilityService : AccessibilityService(), KodeinAware {

    /** We will save the package name to this variable from the event. */
    companion object {
        var currentPackage: CharSequence? = null
    }

    private val keyboardHeight: MutableLiveData<Int> = MutableLiveData()

    private fun postKeyboardValue(value: Int) {
        if (keyboardHeight.value != value) keyboardHeight.postValue(value)
    }

    private lateinit var suggestionButton: Button

    //   private lateinit var windowParams: WindowManager.LayoutParams
    override val kodein by kodein()
    private val repository by instance<MainRepository>()
    private val firebaseUtils by instance<FirebaseUtils>()
    private val clipboardProvider by instance<ClipboardProvider>()

    private var ACCESS_NODE_INFO: AccessibilityNodeInfo? = null

    private val TAG = javaClass.simpleName

    override fun onCreate() {
        super.onCreate()

        firebaseUtils.observeDatabaseChangeEvents()
    }

    private fun supportedEventTypes(event: AccessibilityEvent?): Boolean {
        return (event?.eventType == AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED
                && event.fromIndex == event.toIndex
                && event.currentItemIndex != -1)

                || (event?.eventType == AccessibilityEvent.TYPE_VIEW_CLICKED
                && event.text != null && (event.contentDescription == "Copy" || event.contentDescription == "Cut"))
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        currentPackage = event?.packageName

        postKeyboardValue(getKeyboardHeight(applicationContext))

        // TODO: A paste hack let's see if it works
        event?.source?.apply {

            if (className == EditText::class.java.name) {
                ACCESS_NODE_INFO = this
                /*refresh()
                val bundle = Bundle()
                val texttoPrint = if (event.eventType == AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED)
                    text else ""

                bundle.putString(
                    AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                    "$texttoPrint $ACCESSIBILITY_ARGUMENT_PASTE_TEXT"
                )
                performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, bundle)
                ACCESSIBILITY_ARGUMENT_PASTE_TEXT = null*/
            }
        }

        /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && supportedEventTypes(event) && !isPackageBlacklisted(
                 event?.packageName
             )
         ) {
             Log.e(TAG, "Running for first time")

             runActivity(FLAG_ACTIVITY_NEW_TASK)
         }*/
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

        //   createATestWindow()

        keyboardHeight.observeForever { value ->
            Log.e(TAG, "Value: $value")

            if (value > 100)
                startService(Intent(applicationContext, BubbleService::class.java))
            else
                stopService(Intent(applicationContext, BubbleService::class.java))

            /*    if (value > 100)
                    showTestWindow()
                else
                    removeTestWindow()*/
        }


        LocalBroadcastManager.getInstance(applicationContext)
            .registerReceiver(object: BroadcastReceiver(){
                override fun onReceive(context: Context?, intent: Intent?) {
                    if (intent?.hasExtra(EXTRA_SERVICE_TEXT) == true) {
                        val pasteData = intent.getStringExtra(EXTRA_SERVICE_TEXT)

                        if (ACCESS_NODE_INFO != null) {
                            with(ACCESS_NODE_INFO!!) {
                                refresh()
                                val bundle = Bundle()
                              /*  val textToPrint = if (ACCESS_NODE_INFO?.itemCount != -1)
                                    text else ""*/
                                val textToPrint = text

                                bundle.putString(
                                    AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                                    "$textToPrint $pasteData"
                                )
                                performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, bundle)
                                //  ACCESSIBILITY_ARGUMENT_PASTE_TEXT = null
                            }
                        }
                    }
                }

            }, IntentFilter(ACTION_INSERT_TEXT))
    }

    private fun showTestWindow() {
        suggestionButton.show()
    }

    private fun removeTestWindow() {
        suggestionButton.collapse()
    }


    private fun createATestWindow() {
        val wm =
            getSystemService(Context.WINDOW_SERVICE) as WindowManager

        suggestionButton = Button(this)
        suggestionButton.text = "Suggestion"
        suggestionButton.setOnClickListener {
            Toast.makeText(applicationContext, "This is a toast", Toast.LENGTH_SHORT).show()
        }
        suggestionButton.backgroundTintList =
            ColorStateList.valueOf(
                ContextCompat.getColor(applicationContext, R.color.colorPrimary)
            )
        suggestionButton.isAllCaps = false
        suggestionButton.layoutParams

        removeTestWindow()

        val windowParams: WindowManager.LayoutParams = if (Build.VERSION.SDK_INT >= 26)
            WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, //Application_overlay
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            )
        else WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_SYSTEM_ALERT, //Application_overlay
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        windowParams.verticalMargin = 0.1f // 0.1f
        windowParams.gravity = Gravity.END
        windowParams.title = "Suggestion Layout"

        wm.addView(suggestionButton, windowParams)
    }

    private fun runActivity(flag: Int) {
        val intent = Intent(this, ChangeClipboardActivity::class.java)
        intent.addFlags(flag)
        startActivity(intent)
    }

    override fun onInterrupt() {}

    /**
     * Returns true if the current package name is not part of blacklist app list.
     */
    fun isPackageBlacklisted(pkg: CharSequence?) =
        App.blackListedApps?.contains(pkg) == true
}