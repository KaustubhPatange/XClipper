package com.kpstv.xclipper.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.os.Build
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.kpstv.xclipper.App.CLIP_DATA
import com.kpstv.xclipper.App.observeFirebase
import com.kpstv.xclipper.data.provider.FirebaseProvider
import com.kpstv.xclipper.data.repository.MainRepository
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance


class ClipboardAccessibilityService : AccessibilityService(), KodeinAware {

    override val kodein by kodein()
    private val repository: MainRepository by instance()
    private val firebaseProvider: FirebaseProvider by instance()

    private val TAG = javaClass.simpleName
    private lateinit var clipboardManager: ClipboardManager

    override fun onCreate() {
        super.onCreate()

        firebaseObserver()
    }

    private fun supportedEventTypes(event: AccessibilityEvent?): Boolean {
        return (event?.eventType == AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED
                && event.fromIndex == event.toIndex
                && event.currentItemIndex != -1)

                || (event?.eventType == AccessibilityEvent.TYPE_VIEW_CLICKED
                && event.text != null && (event.contentDescription == "Copy"|| event.contentDescription == "Cut"))
    }


    override fun onAccessibilityEvent(event: AccessibilityEvent?) {

        //   if (event?.eventType == AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED)
        //  Log.e(TAG, "Event: $event")
      //  val condition2 = !isRunning(this)

        //Log.e(TAG, "Condition Running: $condition2, Condition Event: $condition3")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q  && supportedEventTypes(event)) {
            Log.e(TAG, "Running for first time")

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

        clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboardManager.addPrimaryClipChangedListener {
            val data = clipboardManager.primaryClip?.getItemAt(0)?.coerceToText(this)?.toString()
            if (data != null && CLIP_DATA != data) {
                CLIP_DATA = data

                repository.updateRepository(CLIP_DATA)
            }

            Log.e(TAG, "Data: ${clipboardManager.primaryClip?.getItemAt(0)?.text}")
        }


    }

    private fun runActivity(flag: Int) {
        val intent = Intent(this, ChangeClipboardActivity::class.java)
        intent.addFlags(flag)
        startActivity(intent)
    }

    override fun onInterrupt() {}

    private fun firebaseObserver() {
        if (!observeFirebase) return
        firebaseProvider.observeDataChange(
            changed = {
                if (observeFirebase)
                    repository.saveClip(it?.Clips?.last())
                Log.e(TAG, "User has changed")
            },
            error = {
                Log.e(TAG, "Error: ${it.message}")
            },
            deviceValidated = {

            }
        )
    }
}