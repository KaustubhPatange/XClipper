package com.kpstv.xclipper.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_MULTIPLE_TASK
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.os.Build
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.kpstv.xclipper.App.CLIP_DATA
import com.kpstv.xclipper.App.observeFirebase
import com.kpstv.xclipper.data.provider.FirebaseProvider
import com.kpstv.xclipper.data.repository.MainRepository
import com.kpstv.xclipper.extensions.Utils.Companion.isRunning
import org.kodein.di.KodeinAware
import org.kodein.di.android.closestKodein
import org.kodein.di.generic.instance


class ClipboardAccessibilityService : AccessibilityService(), KodeinAware {

    override val kodein by closestKodein()
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
                && event.text != null)
    }



    override fun onAccessibilityEvent(event: AccessibilityEvent?) {

     //   if (event?.eventType == AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED)
          //  Log.e(TAG, "Event: $event")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&!isRunning(this) && supportedEventTypes(event)) {
            Log.e(TAG, "Running for first time")
            val intent = Intent(this, ChangeClipboardActivity::class.java)
            intent.addFlags(FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_MULTIPLE_TASK)
            startActivity(intent)
        }

    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.e(TAG, "Service Connected")
        val info = AccessibilityServiceInfo()
        info.apply {
            eventTypes = AccessibilityEvent.TYPES_ALL_MASK
            feedbackType = AccessibilityServiceInfo.FEEDBACK_ALL_MASK
            notificationTimeout = 100
        }
        serviceInfo = info

        clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboardManager.addPrimaryClipChangedListener {
            val data = clipboardManager.primaryClip?.getItemAt(0)?.text?.toString()
            if (data != null && CLIP_DATA != data) {
                CLIP_DATA = data
                // Save data and exit

                repository.updateRepository(CLIP_DATA)
            }
            Log.e(TAG, "Data: ${clipboardManager.primaryClip?.getItemAt(0)?.text}")
        }
    }

    override fun onInterrupt() { }

    private fun firebaseObserver() {
        if (!observeFirebase) return
        firebaseProvider.observeDataChange(
            changed = {
                if (observeFirebase)
                    repository.saveClip(it?.Clips?.last())
            },
            error = {
                Log.e(TAG, "Error: ${it.message}")
            },
            deviceValidated = {

            }
        )
    }
}