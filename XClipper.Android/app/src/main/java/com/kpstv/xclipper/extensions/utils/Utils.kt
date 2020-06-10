package com.kpstv.xclipper.extensions.utils

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.accessibility.AccessibilityManager
import androidx.annotation.AttrRes
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ShareCompat
import androidx.preference.PreferenceManager
import com.google.zxing.integration.android.IntentIntegrator
import com.kpstv.license.Decrypt
import com.kpstv.xclipper.App
import com.kpstv.xclipper.App.BIND_PREF
import com.kpstv.xclipper.App.BLACKLIST_PREF
import com.kpstv.xclipper.App.EMPTY_STRING
import com.kpstv.xclipper.App.UID_PREF
import com.kpstv.xclipper.R
import com.kpstv.xclipper.data.model.AppPkg
import com.kpstv.xclipper.data.model.Clip
import com.kpstv.xclipper.data.provider.PreferenceProvider
import com.kpstv.xclipper.service.ClipboardAccessibilityService
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.dialog_connect.view.*
import kotlinx.android.synthetic.main.dialog_progress_view.view.*
import java.util.*


class Utils {
    companion object {
        fun isRunning(ctx: Context): Boolean {
            val activityManager =
                ctx.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val tasks =
                activityManager.getRunningTasks(Int.MAX_VALUE)
            for (task in tasks) {
                if ("com.kpstv.xclipper.service.ChangeClipboardActivity".equals(
                        task.baseActivity!!.className,
                        ignoreCase = true
                    )
                ) return true
            }
            return false
        }

        fun shareText(context: Activity, clip: Clip) {
            val intent = ShareCompat.IntentBuilder.from(context)
                .setChooserTitle(context.getString(R.string.share))
                .setType("text/plain")
                .setText(clip.data?.Decrypt())
                .intent
            val shareIntent = Intent.createChooser(intent, null)
            context.startActivity(shareIntent)
        }

        /**
         * Always pass this@Activity as context.
         * Else it won't resolve theme
         */
        fun getColorFromAttr(
            context: Context,
            @AttrRes attrColor: Int,
            typedValue: TypedValue = TypedValue(),
            resolveRefs: Boolean = true
        ): Int {
            context.theme.resolveAttribute(attrColor, typedValue, resolveRefs)
            return typedValue.data
        }

        /** I am too lazy to write my own code.
         *
         *  Source: https://stackoverflow.com/a/31583695/10133501
         */
        fun getCountryDialCode(context: Context): String? {
            var countryId: String? = null
            var contryDialCode: String? = null
            val telephonyMngr =
                context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            countryId = telephonyMngr.simCountryIso.toUpperCase(Locale.ROOT)
            val arrContryCode: Array<String> =
                context.resources.getStringArray(R.array.DialingCountryCode)
            for (i in arrContryCode.indices) {
                val arrDial =
                    arrContryCode[i].split(",").toTypedArray()
                if (arrDial[1].trim { it <= ' ' } == countryId.trim()) {
                    contryDialCode = arrDial[0]
                    break
                }
            }
            return contryDialCode
        }

        fun isPackageInstalled(
            context: Context,
            packageName: String
        ): Boolean {
            return try {
                context.packageManager.getPackageInfo(packageName, 0)
                true
            } catch (e: PackageManager.NameNotFoundException) {
                false
            }
        }


        /**
         * Returns true if the current package name is not part of blacklist app list.
         */
        fun isPackageBlacklisted(pkg: CharSequence?) =
            App.blackListedApps?.contains(pkg) == true

        /**
         * Checks if clipboard accessibility service running or not.
         */
        fun isClipboardAccessibilityServiceRunning(context: Context) =
            isAccessibilityServiceEnabled(context, ClipboardAccessibilityService::class.java)

        /**
         * This will check if accessibility service is enabled or not.
         *
         * @param service Provide the accessibility service class of which you want to
         * detect.
         */
        fun isAccessibilityServiceEnabled(
            context: Context,
            service: Class<out AccessibilityService?>
        ): Boolean {
            val am =
                context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
            val enabledServices =
                am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
            for (enabledService in enabledServices) {
                val enabledServiceInfo: ServiceInfo = enabledService.resolveInfo.serviceInfo
                if (enabledServiceInfo.packageName == context.packageName && enabledServiceInfo.name == service.name
                ) return true
            }
            return false
        }

        /**
         * This will create and show dialog to user to enable accessibility service
         * to make clipboard capturing work even for the Android 10.
         */
        fun showAccessibilityDialog(context: Context, block: () -> Unit): Unit = with(context) {
            AlertDialog.Builder(this)
                .setMessage("In order to capture clipboard clips you need to enable service from accessibility service.\n\n1. Click on to open accessibility settings.\n2. Search for XClipper.\n3. Click and enable the service.")
                .setPositiveButton(getString(R.string.ok)) { _, _ ->
                    openAccessibility(this)

                    block.invoke()
                }
                .setCancelable(false)
                .setNegativeButton(getString(R.string.cancel)) { _, _ -> block.invoke() }
                .show()
        }

        fun openAccessibility(context: Context) = with(context) {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                flags = FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(intent)
        }

        /**
         * Call this function whenever you want to work with installed apps.
         */
        fun retrievePackageList(context: Context): Unit = with(context) {

            if (App.appList.isNotEmpty()) return@with

            packageManager.getInstalledApplications(PackageManager.GET_META_DATA).filter {
                it.flags != ApplicationInfo.FLAG_SYSTEM
            }
                .mapNotNull {
                    AppPkg(
                        it.loadLabel(packageManager),
                        it.packageName
                    )
                }
                .filter { it.label != null && !it.label.contains(".") && it.packageName != null }
                .sortedBy { it.label.toString() }
                .distinctBy { it.label.toString() }
                .also {
                    App.appList.clear()
                    App.appList.addAll(it)
                }


            App.blackListedApps =
                PreferenceManager.getDefaultSharedPreferences(this)
                    .getStringSet(BLACKLIST_PREF, mutableSetOf())
        }

        /**
         * This will show connection dialog and from there we can initiate QR scanning.
         */
        fun showConnectDialog(activity: Activity): Unit = with(activity) {
            val view = LayoutInflater.from(this).inflate(R.layout.dialog_connect, null)

            val alert = AlertDialog.Builder(this)
                .setView(view)
                .show()

            view.btn_scan_connect.setOnClickListener {
                IntentIntegrator(this)
                    .setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
                    .setOrientationLocked(false)
                    .setBeepEnabled(false)
                    .setPrompt(getString(R.string.scan_code))
                    .setBarcodeImageEnabled(false)
                    .initiateScan()
                alert.dismiss()
            }
        }

        fun showConnectionDialog(context: Context): AlertDialog = with(context) {
            val view = LayoutInflater.from(this).inflate(R.layout.dialog_progress_view, null)

            val dialog = AlertDialog.Builder(this)
                .setCancelable(false)
                .setView(view)
                .show()

            view.btn_cancel.setOnClickListener {
                dialog.dismiss()
            }

            return dialog
        }

        fun logoutFromDatabase(preferenceProvider: PreferenceProvider) {
            preferenceProvider.putBooleanKey(BIND_PREF, false)
            preferenceProvider.putStringKey(UID_PREF, EMPTY_STRING)

            App.UID = EMPTY_STRING
            App.BindToFirebase = false
        }

        fun loginToDatabase(preferenceProvider: PreferenceProvider, UID: String) {
            preferenceProvider.putStringKey(UID_PREF, UID)
            preferenceProvider.putBooleanKey(BIND_PREF, true)

            App.UID = UID
            App.BindToFirebase = true
        }



        /* @JvmStatic
         fun cafeBarToast(context: Context, message: String, buttonText: String, block: (CafeBar) -> Unit): CafeBar {
             return CafeBar.builder(context)
                 .content(message)
                 .floating(true)
                 .duration(CafeBar.Duration.INDEFINITE)
                 .neutralText(buttonText)
                 .onNeutral {
                    block.invoke(it)
                 }
                 .autoDismiss(false)
                 .showShadow(true)
                 .build()
         }*/
    }
}