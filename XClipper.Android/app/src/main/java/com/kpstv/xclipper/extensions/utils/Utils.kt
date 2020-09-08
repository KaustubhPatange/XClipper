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
import android.os.Build
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.TypedValue
import android.view.accessibility.AccessibilityManager
import androidx.annotation.AttrRes
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ShareCompat
import androidx.preference.PreferenceManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.zxing.integration.android.IntentIntegrator
import com.kpstv.xclipper.App
import com.kpstv.xclipper.App.AUTO_SYNC_PREF
import com.kpstv.xclipper.App.BIND_DELETE_PREF
import com.kpstv.xclipper.App.BIND_PREF
import com.kpstv.xclipper.App.BLACKLIST_PREF
import com.kpstv.xclipper.R
import com.kpstv.xclipper.data.localized.FBOptions
import com.kpstv.xclipper.data.model.AppPkg
import com.kpstv.xclipper.data.model.Clip
import com.kpstv.xclipper.data.provider.DBConnectionProvider
import com.kpstv.xclipper.data.provider.PreferenceProvider
import com.kpstv.xclipper.databinding.DialogConnectBinding
import com.kpstv.xclipper.databinding.DialogProgressViewBinding
import com.kpstv.xclipper.extensions.SimpleFunction
import com.kpstv.xclipper.extensions.layoutInflater
import com.kpstv.xclipper.service.ClipboardAccessibilityService
import com.kpstv.xclipper.ui.helpers.AuthenticationHelper
import java.io.InputStream
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
                .setText(clip.data)
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
        fun showAccessibilityDialog(context: Context, block: SimpleFunction): Unit = with(context) {
            AlertDialog.Builder(this)
                .setMessage(context.getString(R.string.accessibility_capture))
                .setPositiveButton(getString(R.string.ok)) { _, _ ->
                    openAccessibility(this)
                    block.invoke()
                }
                .setCancelable(false)
                .setNegativeButton(getString(R.string.cancel)) { _, _ -> block.invoke() }
                .show()
        }

        fun showAccessibilityDialog(context: Context) {
            showAccessibilityDialog(context) { }
        }

        fun openAccessibility(context: Context) = with(context) {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                flags = FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(intent)
        }

        @RequiresApi(Build.VERSION_CODES.M)
        fun showOverlayDialog(context: Context) = with(context) {
            AlertDialog.Builder(this)
                .setMessage(getString(R.string.suggestion_capture))
                .setPositiveButton(getString(R.string.ok)) { _, _ ->
                    openSystemOverlay(this)
                }
                .setCancelable(false)
                .setNegativeButton(getString(R.string.cancel), null)
                .show()
        }

        fun isSystemOverlayEnabled(context: Context): Boolean = with(context) {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Settings.canDrawOverlays(this)
            } else true
        }

        fun isAndroid10orUp() =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

        @RequiresApi(Build.VERSION_CODES.M)
        fun openSystemOverlay(context: Context) = with(context) {
            val myIntent =
                Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                    flags = FLAG_ACTIVITY_NEW_TASK
                }
            startActivity(myIntent)
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
            val binding = DialogConnectBinding.inflate(layoutInflater)

            val alert = AlertDialog.Builder(this)
                .setView(binding.root)
                .show()

            binding.btnScanConnect.setOnClickListener {
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
            val binding = DialogProgressViewBinding.inflate(layoutInflater())

            val dialog = AlertDialog.Builder(this)
                .setCancelable(false)
                .setView(binding.root)
                .show()

            binding.btnCancel.setOnClickListener {
                dialog.dismiss()
            }

            return dialog
        }

        fun logoutFromDatabase(
            context: Context,
            preferenceProvider: PreferenceProvider,
            dbConnectionProvider: DBConnectionProvider
        ) {
            dbConnectionProvider.optionsProvider()?.apply {
                if (isAuthNeeded) {
                    Firebase.auth.signOut()
                    AuthenticationHelper.signOutGoogle(context, authClientId)
                }
            }
            dbConnectionProvider.detachDataFromAll()
            preferenceProvider.putBooleanKey(BIND_PREF, false)
            preferenceProvider.putBooleanKey(AUTO_SYNC_PREF, false)
            preferenceProvider.putBooleanKey(BIND_DELETE_PREF, false)
            App.bindToFirebase = false
            App.runAutoSync = false
            App.bindDelete = false
        }

        fun loginToDatabase(
            preferenceProvider: PreferenceProvider,
            dbConnectionProvider: DBConnectionProvider,
            options: FBOptions
        ) {
            dbConnectionProvider.saveOptionsToAll(options)
            preferenceProvider.putBooleanKey(BIND_PREF, true)
            preferenceProvider.putBooleanKey(AUTO_SYNC_PREF, true)
            App.bindToFirebase = true
            App.runAutoSync = true
        }

        fun isValidSQLite(stream: InputStream?): Boolean {
            val buffer = CharArray(16)
            stream?.bufferedReader()?.read(buffer, 0, 16)
            val str = String(buffer)
            return str == "SQLite format 3\u0000"
        }
    }
}