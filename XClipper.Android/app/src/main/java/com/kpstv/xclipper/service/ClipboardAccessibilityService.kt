package com.kpstv.xclipper.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.os.Build
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.kpstv.xclipper.App
import com.kpstv.xclipper.App.CLIP_DATA
import com.kpstv.xclipper.App.observeFirebase
import com.kpstv.xclipper.R
import com.kpstv.xclipper.data.provider.FirebaseProvider
import com.kpstv.xclipper.data.provider.PreferenceProvider
import com.kpstv.xclipper.data.repository.MainRepository
import com.kpstv.xclipper.extensions.utils.Utils.Companion.logoutFromDatabase
import com.kpstv.xclipper.extensions.utils.Utils.Companion.retrievePackageList
import es.dmoral.toasty.Toasty
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance


class ClipboardAccessibilityService : AccessibilityService(), KodeinAware {

    /** We will save the package name to this variable from the event. */
    private var currentPackage: CharSequence? = null

    override val kodein by kodein()
    private val repository by instance<MainRepository>()
    private val firebaseProvider by instance<FirebaseProvider>()
    private val preferenceProvider by instance<PreferenceProvider>()

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
                && event.text != null && (event.contentDescription == "Copy" || event.contentDescription == "Cut"))
    }


    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        currentPackage = event?.packageName

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && supportedEventTypes(event) && !isPackageBlacklisted(
                event?.packageName
            )
        ) {
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

            if (isPackageBlacklisted(currentPackage)) return@addPrimaryClipChangedListener

            val data = clipboardManager.primaryClip?.getItemAt(0)?.coerceToText(this)?.toString()
            if (data != null && CLIP_DATA != data) {
                CLIP_DATA = data

                repository.updateRepository(CLIP_DATA)
            }

            Log.e(TAG, "Data: ${clipboardManager.primaryClip?.getItemAt(0)?.text}")
        }

        retrievePackageList(applicationContext)
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
    private fun isPackageBlacklisted(pkg: CharSequence?) =
        App.blackListedApps?.contains(pkg) == true

    private fun firebaseObserver() {
        if (!observeFirebase) return
        firebaseProvider.observeDataChange(
            changed = {
                if (observeFirebase)
                    repository.updateClip(it?.Clips?.last())
                Log.e(TAG, "User has changed")
            },
            error = {
                Log.e(TAG, "Error: ${it.message}")
            },
            deviceValidated = { isValidated ->

                if (!isValidated) {
                    logoutFromDatabase(preferenceProvider)
                    Toasty.error(
                        applicationContext,
                        getString(R.string.err_device_validate),
                        Toasty.LENGTH_LONG
                    ).show()
                } else {

                }
            }
        )
    }
}